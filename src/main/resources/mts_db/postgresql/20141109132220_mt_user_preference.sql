--
-- The MTS core EJB project is the base framework for the CDS Framework Middle Tier Service.
--
-- Copyright (C) 2016 New York City Department of Health and Mental Hygiene, Bureau of Immunization
-- Contributions by HLN Consulting, LLC
--
-- This program is free software: you can redistribute it and/or modify it under the terms of the GNU
-- Lesser General Public License as published by the Free Software Foundation, either version 3 of the
-- License, or (at your option) any later version. You should have received a copy of the GNU Lesser
-- General Public License along with this program. If not, see <http://www.gnu.org/licenses/> for more
-- details.
--
-- The above-named contributors (HLN Consulting, LLC) are also licensed by the New York City
-- Department of Health and Mental Hygiene, Bureau of Immunization to have (without restriction,
-- limitation, and warranty) complete irrevocable access and rights to this project.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; THE
-- SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING,
-- BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
-- NONINFRINGEMENT. IN NO EVENT SHALL THE COPYRIGHT HOLDERS, IF ANY, OR DEVELOPERS BE LIABLE FOR
-- ANY CLAIM, DAMAGES, OR OTHER LIABILITY OF ANY KIND, ARISING FROM, OUT OF, OR IN CONNECTION WITH
-- THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
--
-- For more information about this software, see https://www.hln.com/services/open-source/ or send
-- correspondence to ice@hln.com.
--

-- // mt_user_preference
-- Migration SQL that makes the change goes here.

create table mt_user_preference (
            preference_id varchar(32) primary key,
            user_id varchar(32) not null,
            name varchar(128) not null,
            value varchar(512),
            type varchar(128) not null,
            session_preference boolean not null default false,
            session_persistent boolean not null default true,
            default_value varchar(512),
            user_editable boolean not null default false,
            last_mod_datetime timestamp not null,
            last_mod_id varchar(32) not null,
            create_datetime timestamp not null,
            create_id varchar(32) not null,
            constraint fk_mt_userp_to_mt_user foreign key (user_id) references mt_user (user_id),
            constraint un_mt_userp_to_mt_user unique (user_id, name));

-- //@UNDO
-- SQL to undo the change goes here.

drop table mt_user_preference;
