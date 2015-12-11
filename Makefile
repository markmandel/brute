#
# Makefile for managing the Docker dev environment and
# the resources within it.
#

#  __     __         _       _     _
#  \ \   / /_ _ _ __(_) __ _| |__ | | ___ ___
#   \ \ / / _` | '__| |/ _` | '_ \| |/ _ \ __|
#    \ V / (_| | |  | | (_| | |_) | |  __\__ \
#     \_/ \__,_|_|  |_|\__,_|_.__/|_|\___|___/
#

TAG=markmandel/brute-dev
NAME=brute-dev

#Directory that this Makefile is in.
mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
current_path := $(dir $(mkfile_path))

#   _____                    _
#  |_   _|_ _ _ __ __ _  ___| |_ ___
#    | |/ _` | '__/ _` |/ _ \ __/ __|
#    | | (_| | | | (_| |  __/ |_\__ \
#    |_|\__,_|_|  \__, |\___|\__|___/
#                 |___/

# build the docker dev image
build:
	docker build --tag=$(TAG) $(current_path)/dev

#clean up the shell
clean:
	docker rmi $(TAG)

# Start a development shell
shell: m2
		docker run --rm \
				--name=$(NAME) \
				-P=true \
				-e HOST_GID=`id -g` \
				-e HOST_UID=`id -u` \
				-e HOST_USER=$(USER) \
				-e DOCKER_GID=$(word 3,$(subst :, ,$(shell getent group docker))) \
				-v ~/.m2:/home/$(USER)/.m2 \
				-v $(current_path)/dev/profiles.clj:/home/$(USER)/.lein/profiles.clj \
				-v $(current_path)/dev/zshrc:/home/$(USER)/.zshrc \
				-v $(current_path):/project \
				-v /usr/bin/docker:/usr/bin/docker \
				-v /var/run/docker.sock:/var/run/docker.sock \
				-it $(TAG) /root/startup.sh

shell-attach:
	docker exec -it --user=$(USER) $(NAME) zsh

# mount the docker's jvm in the /tmp dir
shell-mount-jvm:
		mkdir -p /tmp/$(NAME)/jvm
		sshfs $(USER)@0.0.0.0:/usr/lib/jvm /tmp/$(NAME)/jvm -p $(call getPort,22) -o follow_symlinks

# Run the tests inside the docker container, and output the results.
test: m2
	docker run --rm \
			-v ~/.m2:/root/.m2 \
			-v $(current_path):/project \
			$(TAG) lein alltest

# make sure the maven local dir is there
m2:
	mkdir -p ~/.m2

# push the image up to docker hub
push:
	docker push $(TAG)

#   ____  _          _ _   _____                    _
#  / ___|| |__   ___| | | |_   _|_ _ _ __ __ _  ___| |_ ___
#  \___ \| '_ \ / _ \ | |   | |/ _` | '__/ _` |/ _ \ __/ __|
#   ___) | | | |  __/ | |   | | (_| | | | (_| |  __/ |_\__ \
#  |____/|_| |_|\___|_|_|   |_|\__,_|_|  \__, |\___|\__|___/
#                                        |___/

wercker-build:
	wercker --verbose --debug build --docker-local --direct-mount --working-dir /tmp

#   _____                 _   _
#  |  ___|   _ _ __   ___| |_(_) ___  _ __  ___
#  | |_ | | | | '_ \ / __| __| |/ _ \| '_ \/ __|
#  |  _|| |_| | | | | (__| |_| | (_) | | | \__ \
#  |_|   \__,_|_| |_|\___|\__|_|\___/|_| |_|___/
#

# get the mapped docker host port
getPort = $(word 2,$(subst :, ,$(shell docker port $(NAME) $(1))))