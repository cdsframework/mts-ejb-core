--
-- The RCKMS EJB cdsframework implementation.
--
-- Copyright 2016 HLN Consulting, LLC
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- For more information about the this software, see https://www.hln.com/services/open-source/ or send
-- correspondence to scm@cdsframework.org.
--

-- // vw_notification_release_note
-- Migration SQL that makes the change goes here.

alter table notification_release_note_file drop constraint fk_nrnf_nid_2_nrn_nid;
alter table notification_release_note_file add constraint fk_nrnf_nid_2_nrn_nid foreign key (note_id) references notification_release_note (note_id) on delete cascade;

-- //@UNDO
-- SQL to undo the change goes here.

alter table notification_release_note_file drop constraint fk_nrnf_nid_2_nrn_nid;
alter table notification_release_note_file add constraint fk_nrnf_nid_2_nrn_nid foreign key (note_id) references notification_release_note (note_id);
