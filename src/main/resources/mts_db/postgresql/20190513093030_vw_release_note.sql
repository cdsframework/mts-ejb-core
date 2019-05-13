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

create or replace view vw_notification_release_note as
    select  nrn.*, (select count(nrnf.*) from notification_release_note_file nrnf where nrnf.note_id = nrn.note_id) as file_count
    from notification_release_note nrn; 

-- //@UNDO
-- SQL to undo the change goes here.

drop view vw_notification_release_note;
