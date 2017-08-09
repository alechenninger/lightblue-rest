/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.rest.metrics;

import org.junit.Assert;
import org.junit.Test;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.redhat.lightblue.rest.crud.metrics.MetricRegistryFactory;
import com.redhat.lightblue.rest.crud.metrics.RequestMetrics;

public class RequestMetricsTest {
    private RequestMetrics requestMetrics = new RequestMetrics();
    
    private static MetricRegistry metricsRegistry = MetricRegistryFactory.getMetricRegistry();;

    @Test
    public void testStartRequestMonitoring() {
        requestMetrics.startRequestMonitoring("save", "name", "version");
        Assert.assertNotNull(metricsRegistry.getCounters());
        Assert.assertNotNull(metricsRegistry.getTimers());

        Counter activeRequestCounter = metricsRegistry.counter("api.save.name.version.requests.active");
        Timer completedRequestTimer = metricsRegistry.timer("api.save.name.version.requests.completed");

        Assert.assertEquals(1, activeRequestCounter.getCount());
        Assert.assertEquals(0, completedRequestTimer.getCount());
        Assert.assertNotNull(completedRequestTimer.getMeanRate());
    }

    @Test
    public void testEndRequestMonitoring() {
        requestMetrics.startRequestMonitoring("explain", "name", "version");
        requestMetrics.endRequestMonitoring();

        Counter activeRequestCounter = metricsRegistry.counter("api.explain.name.version.requests.active");
        Timer completedRequestTimer = metricsRegistry.timer("api.explain.name.version.requests.completed");

        Assert.assertEquals(0, activeRequestCounter.getCount());
        Assert.assertEquals(1, completedRequestTimer.getCount());
        Assert.assertNotNull(completedRequestTimer.getOneMinuteRate());
    }

    @Test
    public void testMarkRequestException() {
        requestMetrics.startRequestMonitoring("insert", "name", "version");        
        requestMetrics.markRequestException(new NullPointerException());
        
        Meter exceptionMeter = metricsRegistry.meter("api.insert.name.version.requests.exception.NullPointerException");
        Assert.assertEquals(1, exceptionMeter.getCount());
    }
}
