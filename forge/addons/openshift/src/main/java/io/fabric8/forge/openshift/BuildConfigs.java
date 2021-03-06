/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.openshift;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.*;
import io.fabric8.utils.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class BuildConfigs {

    public static Map<String, String> createBuildLabels(String buildName) {
        Map<String, String> answer = new HashMap<>();
        answer.put("name", buildName);
        return answer;
    }

    public static ImageStream imageRepository(String buildName, Map<String, String> labels) {
        return new ImageStreamBuilder().
                withLabels(labels).
                withName(buildName).
                build();
    }

    public static BuildConfigSpec addBuildParameterOutput(BuildConfigSpecBuilder builder, String imageTag) {
        return builder.
                withNewOutput().
                withTag(imageTag).
                // TODO add to / name  on output
                        endOutput().
                build();
    }

    public static BuildConfigSpec addBuildParameterGitSource(BuildConfigSpecBuilder builder, String gitUrl) {
        return builder.
                withNewSource().
                withType("Git").
                withNewGit().withUri(gitUrl).endGit().
                endSource().
                build();
    }

    public static BuildConfigSpec addBuildConfigSpectiStrategy(BuildConfigSpecBuilder builder, String image) {
        return builder.
                withNewStrategy().
                withType("STI").
                // TODO add builderImage
                        withNewStiStrategy().withImage(image).
                endStiStrategy().
                endStrategy().
                build();
    }


    public static BuildConfigSpec addBuildParameterCustomStrategy(BuildConfigSpecBuilder builder, String image, List<EnvVar> envVars) {
        return builder.
                withNewStrategy().
                withType("Custom").
                withNewCustomStrategy().
                withImage(image).
                withEnv(envVars).
                endCustomStrategy().
                endStrategy().
                build();
    }


    public static BuildConfigBuilder buildConfigBuilder(String buildName, Map<String, String> labels, BuildConfigSpec parameters) {
        return buildConfigBuilder(buildName, labels).
                withParameters(parameters);
    }

    public static BuildConfigBuilder addWebHookTriggers(BuildConfigBuilder builder, String secret) {
        return builder.
                addNewTrigger().
                withType("github").
                withNewGithub().withSecret(secret).endGithub().
                endTrigger().

                addNewTrigger().
                withType("generic").
                withNewGeneric().withSecret(secret).endGeneric().
                endTrigger();
    }

    public static BuildConfigBuilder buildConfigBuilder(String buildName, Map<String, String> labels) {
        return new BuildConfigBuilder().
                withLabels(labels).
                withName(buildName);
    }

    public static BuildConfig createBuildConfig(String buildConfigName, Map<String, String> labels, String gitUrlText, String outputImageTagText, String imageText, String webhookSecret) {
        BuildConfigSpecBuilder parametersBuilder = new BuildConfigSpecBuilder();
        addBuildParameterGitSource(parametersBuilder, gitUrlText);
        if (Strings.isNotBlank(outputImageTagText)) {
            addBuildParameterOutput(parametersBuilder, outputImageTagText);
        }
        if (Strings.isNotBlank(imageText)) {
            addBuildConfigSpectiStrategy(parametersBuilder, imageText);
        }
        BuildConfigBuilder builder = buildConfigBuilder(buildConfigName, labels, parametersBuilder.build());
        if (Strings.isNotBlank(webhookSecret)) {
            addWebHookTriggers(builder, webhookSecret);
        }
        return builder.build();
    }

    public static BuildConfig createIntegrationTestBuildConfig(String buildConfigName, Map<String, String> labels, String gitUrlText, String image, List<EnvVar> envVars) {
        BuildConfigSpecBuilder parametersBuilder = new BuildConfigSpecBuilder();
        addBuildParameterGitSource(parametersBuilder, gitUrlText);
        if (Strings.isNotBlank(image)) {
            addBuildParameterCustomStrategy(parametersBuilder, image, envVars);
        }
        BuildConfigBuilder builder = buildConfigBuilder(buildConfigName, labels, parametersBuilder.build());
        return builder.build();
    }
}
