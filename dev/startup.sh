#!/usr/bin/env sh

groupadd --gid $HOST_GID $HOST_USER
useradd $HOST_USER --home /home/$HOST_USER --gid $HOST_GID --uid $HOST_UID --shell /usr/bin/zsh
echo "$HOST_USER:pw" | chpasswd

#zsh
chown -R $HOST_USER:$HOST_USER /oh-my-zsh

#setup lein for this user
cp -r /root/.lein/* /home/$HOST_USER/.lein/

#make sure all permissions are good to go.
chown -R $HOST_USER:$HOST_USER /home/$HOST_USER

#allow docker passthrough
groupadd --gid $DOCKER_GID docker
usermod -a -G docker $HOST_USER

/usr/sbin/sshd
su $HOST_USER