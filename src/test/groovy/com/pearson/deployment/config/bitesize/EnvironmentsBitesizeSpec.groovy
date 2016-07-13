package com.pearson.deployment.config.bitesize

import spock.lang.*
import groovy.mock.interceptor.MockFor
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import java.io.*
import java.lang.*

import org.yaml.snakeyaml.constructor.ConstructorException

class EnvironmentsBitesizeSpec extends Specification {
    String e
    String eInvalid

    def setup() {
        e = new File("src/test/resources/config/environments.bitesize").text
        eInvalid = """
        project: sss
        environments:
          -name: development
          namespace: ooo
        """
    }

    def "valid config" () {
      when:
        def cfg = EnvironmentsBitesize.readConfigFromString(e)
        def stagingEnvironment = cfg.getEnvironment('Staging')

      then:
        cfg.project == "example"
        cfg.environments.size() == 2
        cfg.environments[0].services[0].port == 80
        cfg.environments[0].services[0].application == 'sample-app'
        cfg.environments[0].services[0].ssl == true
        cfg.environments[1] == stagingEnvironment
    }

    def "invalid config" () {
      when:
        def cfg = EnvironmentsBitesize.readConfigFromString(eInvalid)

      then:
        ConstructorException ex = thrown()
    }

    def "we can get environment" () {
      given:
        def cfg = EnvironmentsBitesize.readConfigFromString(e)

      when:
        def staging = cfg.getEnvironment('Staging')
      then:
        cfg.environments[1] == staging

      when:
        def z = cfg.getEnvironment('Zoro')        
      then:
        EnvironmentNotFoundException ex = thrown()
        ex.message == 'Environment Zoro not found'

    }

}