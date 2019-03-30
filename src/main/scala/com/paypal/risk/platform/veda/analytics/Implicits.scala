/**
 * Copyright 2019 PayPal Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.risk.platform.veda.analytics

import com.paypal.risk.platform.veda.analytics.anomaly.{AnomalyWorkload, ColumnKey, Report, WindowRange}
import org.apache.spark.sql.{DataFrame, Dataset}

object Implicits {

  /** extension to the DataFrame API e.g. allowing df.detectAnomalies(..) */
  implicit class AnomalyDatasetWrapper(df: DataFrame) {
    def detectAnomalies(workload: AnomalyWorkload): Dataset[(ColumnKey, Report, WindowRange)] = {
      workload.process(df)
    }

    /** extension to the DataFrame API e.g. allowing df.cluster(..) */
    def cluster(workload: AnomalyWorkload): Dataset[(ColumnKey, Report, WindowRange)] = {
      workload.process(df)
    }
  }
}
