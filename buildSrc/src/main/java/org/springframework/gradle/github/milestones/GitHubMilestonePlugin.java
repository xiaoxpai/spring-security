/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.gradle.github.milestones;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class GitHubMilestonePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		TaskProvider<GitHubMilestoneNextReleaseTask> nextReleaseMilestoneTask = project.getTasks().register("gitHubNextReleaseMilestone", GitHubMilestoneNextReleaseTask.class, new Action<GitHubMilestoneNextReleaseTask>() {
					@Override
					public void execute(GitHubMilestoneNextReleaseTask gitHubMilestoneNextReleaseTask) {
						gitHubMilestoneNextReleaseTask.doNotTrackState("API call to GitHub needs to check for new milestones every time");
						gitHubMilestoneNextReleaseTask.setGroup("Release");
						gitHubMilestoneNextReleaseTask.setDescription("Calculates the next release version based on the current version and outputs it to a yaml file");
						gitHubMilestoneNextReleaseTask.getNextReleaseFile()
								.fileProvider(project.provider(() -> project.file("next-release.yml")));
						if (project.hasProperty("gitHubAccessToken")) {
							gitHubMilestoneNextReleaseTask
									.setGitHubAccessToken((String) project.findProperty("gitHubAccessToken"));
						}
					}
				});
		project.getTasks().register("gitHubCheckMilestoneHasNoOpenIssues", GitHubMilestoneHasNoOpenIssuesTask.class, new Action<GitHubMilestoneHasNoOpenIssuesTask>() {
			@Override
			public void execute(GitHubMilestoneHasNoOpenIssuesTask githubCheckMilestoneHasNoOpenIssues) {
				githubCheckMilestoneHasNoOpenIssues.setGroup("Release");
				githubCheckMilestoneHasNoOpenIssues.setDescription("Checks if there are any open issues for the specified repository and milestone");
				githubCheckMilestoneHasNoOpenIssues.setMilestoneTitle((String) project.findProperty("nextVersion"));
				if (!project.hasProperty("nextVersion")) {
					githubCheckMilestoneHasNoOpenIssues.getNextVersionFile().convention(
							nextReleaseMilestoneTask.flatMap(GitHubMilestoneNextReleaseTask::getNextReleaseFile));
				}
				if (project.hasProperty("gitHubAccessToken")) {
					githubCheckMilestoneHasNoOpenIssues.setGitHubAccessToken((String) project.findProperty("gitHubAccessToken"));
				}
			}
		});
		project.getTasks().register("gitHubCheckNextVersionDueToday", GitHubMilestoneNextVersionDueTodayTask.class, new Action<GitHubMilestoneNextVersionDueTodayTask>() {
					@Override
					public void execute(GitHubMilestoneNextVersionDueTodayTask gitHubMilestoneNextVersionDueTodayTask) {
						gitHubMilestoneNextVersionDueTodayTask.setGroup("Release");
						gitHubMilestoneNextVersionDueTodayTask.setDescription("Checks if the next release version is due today or past due, will fail if the next version is not due yet");
						gitHubMilestoneNextVersionDueTodayTask.getNextVersionFile().convention(
								nextReleaseMilestoneTask.flatMap(GitHubMilestoneNextReleaseTask::getNextReleaseFile));
						if (project.hasProperty("gitHubAccessToken")) {
							gitHubMilestoneNextVersionDueTodayTask
									.setGitHubAccessToken((String) project.findProperty("gitHubAccessToken"));
						}
					}
				});
	}
}
