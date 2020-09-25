#   Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA

#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
 
#       http://www.apache.org/licenses/LICENSE-2.0
 
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

require_relative '../../Synthax/Attributes/enum_case.rb'
require_relative '../base_component.rb'
require_relative '../../Synthax/Types/built_in_type.rb'

class PositionType < BaseComponent

    def initialize
        enum_cases = [
            EnumCase.new(:name => "relative", :defaultValue => "RELATIVE"),
            EnumCase.new(:name => "absolute", :defaultValue => "ABSOLUTE")
        ]
        synthax_type = EnumType.new(
            :name => self.name,
            :variables => enum_cases,
            :package => "br.com.zup.beagle.core",
            :inheritFrom => [BasicTypeKeys.string]
        )

        super(synthax_type)

    end

end