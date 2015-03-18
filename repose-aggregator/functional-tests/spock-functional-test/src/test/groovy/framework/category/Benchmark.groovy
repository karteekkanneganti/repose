/*
 *  Copyright (c) 2015 Rackspace US, Inc.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package framework.category

/*
 * This interface is used to associate spock tests to a category described by the class name.
 *
 * A test is considered a benchmark test if it is used to gauge run-time metrics.
 * In other words, a benchmark test introduces load into a system and quantitatively measures
 * the impact (e.g., throughput, response time, number of errors, etc.).
 */
public interface Benchmark extends Slow {}
