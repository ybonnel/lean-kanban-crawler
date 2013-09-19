/*
 * Copyright 2013- Yan Bonnel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ybonnel.crawler;

import fr.ybonnel.modele.Schedule;
import fr.ybonnel.modele.Speaker;
import fr.ybonnel.simpleweb4j.test.SimpleWeb4jTest;
import org.fluentlenium.core.domain.FluentWebElement;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LanyrdCrawler extends SimpleWeb4jTest {

    private static LanyrdCrawler instance = new LanyrdCrawler();

    public static LanyrdCrawler getInstance() {
        return instance;
    }

    private List<Schedule> cachesSchedules = null;

    @Override
    protected String defaultUrl() {
        return "http://lanyrd.com/2013/lean-kanban-france/schedule/";
    }

    public List<Schedule> crawl() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (cachesSchedules != null) {
            return cachesSchedules;
        }
        Method starting = TestWatcher.class.getDeclaredMethod("starting", Description.class);
        starting.setAccessible(true);
        starting.invoke(this.lifecycle, Description.EMPTY);
        goTo("/");
        List<Schedule> schedules = new ArrayList<>();

        List<Speaker> allSpeakers = new ArrayList<>();
        for (FluentWebElement scheduleItem : find("li.schedule-item")) {
            Schedule schedule = new Schedule();
            schedule.setTitle(scheduleItem.find("h2 a").getText());
            schedule.setUrl(scheduleItem.find("h2 a").getAttribute("href"));
            schedule.setDescription(scheduleItem.find("div.desc").getText());

            String startDate = scheduleItem.find("span.dtstart span.value-title").getAttribute("title");
            String endDate = scheduleItem.find("span.dtend span.value-title").getAttribute("title");

            schedule.setBeginDate(new LocalDateTime(startDate.replace("+ZZ:ZZ", ""), DateTimeZone.forID("Europe/Paris")).toDate());
            schedule.setEndDate(new LocalDateTime(endDate.replace("+ZZ:ZZ", ""), DateTimeZone.forID("Europe/Paris")).toDate());

            schedule.setRoom(scheduleItem.find("div.schedule-meta div p", 1).getText().split("\n")[1].split(",")[0]);

            schedules.add(schedule);
        }

        for (Schedule schedule : schedules) {
            goTo(schedule.getUrl());
            for (FluentWebElement profileItem : find("div.primary div.mini-profile")) {
                Speaker speaker = new Speaker();
                speaker.setAvatar(profileItem.find("div.avatar a img").getAttribute("src"));
                speaker.setName(profileItem.find("span.name a").getText());
                speaker.setBio(profileItem.find("div.profile-longdesc p").getText());
                schedule.getSpeakers().add(speaker);
            }

        }
        cachesSchedules = schedules;
        return schedules;
    }
}
