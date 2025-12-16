/**
 *    Copyright 2019 Sven Loesekann
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
@org.springframework.modulith.ApplicationModule(
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {"stocks", "stocks :: stocks.dto", "stocks :: stocks.mapping.open",
                "stocks :: stocks.entity.dto", "stocks :: stocks.entity", "findata", "findata :: findata.dto" }
)
package ch.xxx.manager.common;

import org.springframework.modulith.ApplicationModule;