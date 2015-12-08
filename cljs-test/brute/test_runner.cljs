(ns brute.test-runner
  "the test runner"
  (:require [doo.runner :refer-macros [doo-tests]]
            [brute.entity-test]
            [brute.system-test]))

(doo-tests 'brute.entity-test
           'brute.system-test)

