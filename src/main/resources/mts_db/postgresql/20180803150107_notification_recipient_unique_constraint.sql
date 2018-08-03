--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // notification recipient unique constraint
-- Migration SQL that makes the change goes here.

alter table notification_recipient drop constraint un_nr_sid_uid_nid;
alter table notification_recipient add constraint un_nr_rt_uid_nid unique (recipient_type, user_id, notification_id);
alter table notification_recipient add constraint un_nr_rt_sid_nid unique (recipient_type, scheme_id, notification_id);

-- //@UNDO
-- SQL to undo the change goes here.

alter table notification_recipient drop constraint un_nr_rt_uid_nid;
alter table notification_recipient drop constraint un_nr_rt_sid_nid;
alter table notification_recipient add constraint un_nr_sid_uid_nid unique (scheme_id, user_id, notification_id);
