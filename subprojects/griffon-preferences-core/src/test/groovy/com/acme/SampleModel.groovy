/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme

import griffon.core.artifact.GriffonModel
import griffon.plugins.preferences.Preference
import griffon.plugins.preferences.PreferencesAware
import org.kordamp.jipsy.ServiceProviderFor

@PreferencesAware
@ServiceProviderFor(GriffonModel)
class SampleModel {
    @Preference
    String pstring

    @Preference(defaultValue = 'true')
    boolean pboolean

    @Preference(defaultValue = '01/01/2000', format = 'dd/MM/yyyy')
    Date pdate

    @Preference(defaultValue = '*value*', converter = CustomStringConverter)
    String customString
}
