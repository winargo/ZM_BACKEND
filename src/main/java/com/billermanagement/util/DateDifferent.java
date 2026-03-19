/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulaeman
 */
public class DateDifferent {

    private final Logger log = LoggerFactory.getLogger(DateDifferent.class);
    private String dateStart;
    private String dateStop;

    public String getDateDifferent(Date dateStart, Date dateStop) {
        String result = null;
//      HH converts hour in 24 hours format (0-23), day calculation
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.dateStart = format.format(dateStart);
        this.dateStop = format.format(dateStop);

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(this.dateStart);
            d2 = format.parse(this.dateStop);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffSeconds > 0) {
                result = diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds.";
            } else {
                result = diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds, " + diff + " milliseconds.";
            }
        } catch (ParseException e) {
            log.error("getDateDifferent error " + e.getMessage());
        }
        return result;
    }

    public String getInMillis(Instant timeStart, Instant timeEnd) {
        String result = null;
        long milliseconds = Duration.between(timeStart, timeEnd).toMillis();
        result = milliseconds + " milliseconds";
        return result;
    }

}
