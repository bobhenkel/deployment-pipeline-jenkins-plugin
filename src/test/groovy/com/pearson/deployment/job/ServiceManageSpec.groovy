package com.pearson.deployment.job

import spock.lang.*
import groovy.mock.interceptor.MockFor
import org.mockito.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import hudson.model.*
import hudson.FilePath

import com.pearson.deployment.kubernetes.*
import com.pearson.deployment.config.*
import com.pearson.deployment.config.bitesize.*


class ServiceManageSpec extends Specification {
    def dummyFile = new MockFor(File)
    @Rule final TemporaryFolder configDir = new TemporaryFolder()

    ServiceManage manager

    def setup() {
      def envConfig = """
        project: sample
        environments:
        - name: development
          namespace: sample-app-dev
          deployment:
            method: rolling-rolling
            timeout: 3000
          services:
            - name: myservice
              ssl: true
              external_url: www.google.com 
      """

      AbstractBuild build = Mockito.mock(AbstractBuild.class)
      BuildListener listener = Mockito.mock(BuildListener.class)
      Mockito.when(listener.getLogger()).thenReturn(System.out)
      
      ServiceManage spied = new ServiceManage(build, listener, 'environments.bitesize')
      manager = Mockito.spy(spied)                 
      manager.config = EnvironmentsBitesize.readConfigFromString(envConfig)

    }

    def "Basic service manage" () {
      when:
      manager.run()
      then:
      manager.project() == "sample"
    }

    def "Service change settings" () {
      given:
      def c = """
        project: sample
        environments:
        - name: development
          namespace: sample-app-dev
          deployment:
            method: rolling-rolling
            timeout: 3000
          services:
            - name: myservice
              ssl: false
              external_url: www.google.co.uk
              port: 81
      """
      manager.config = EnvironmentsBitesize.readConfigFromString(c)

      def serviceWrap = new KubeWrapper('service', 'sample-app-dev')
      def deploymentWrap = new KubeWrapper('deployment', 'sample-app-dev')
      when:
      manager.run()
      def svc = serviceWrap.fetch('myservice')
      def h = new KubeServiceHandler(svc)
      then:
      h.svc.port == 81
    }

}
