package com.sebulli.fakturama.misc;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;


public class DateFormatterTest {
//
//    @Test
//    public void testDateAndTimeAsLocalString() {
//        IDateFormatterService formatter = new DateFormatter();
//        Assert.assertEquals("Date is invalid!", "Mittwoch, 20. Mai 2020 18:57:00", formatter.DateAndTimeAsLocalString("2020-05-20 18:57:00"));
//
//    }
    //
    //    @Test
    //    public void testDateAsISO8601StringString() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testDateAsISO8601String() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testDateAndTimeOfNowAsISO8601String() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetDateAndTimeAsString() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetDateTimeAsStringCalendar() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetDateTimeAsLocalString() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetFormattedLocalizedDate() {
    //        fail("Not yet implemented");
    //    }
    
        @Test
        public void testGetCalendarFromDateString() {
            IDateFormatterService formatter = new DateFormatter();
            Calendar testCal = Calendar.getInstance();
            testCal.clear();
            testCal.set(2020, 1, 20, 0,0,0);
            
            Assert.assertEquals(testCal, formatter.getCalendarFromDateString("2020-02-20"));
            Assert.assertEquals(testCal, formatter.getCalendarFromDateString("20.02.2020"));
            Assert.assertEquals(testCal, formatter.getCalendarFromDateString("02/20/2020"));
        }
    
    //    @Test
    //    public void testDateAndTimeOfNowAsLocalString() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testGetDateTimeAsStringDateTime() {
    //        fail("Not yet implemented");
    //    }
    //
}
