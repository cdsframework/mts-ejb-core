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

-- // mt_security_scheme
-- Migration SQL that makes the change goes here.

create table mt_security_scheme (
            scheme_id varchar(32) primary key,
            scheme_name varchar(40) not null,
            description varchar(256),
            last_mod_datetime timestamp not null,
            last_mod_id varchar(32) not null,
            create_datetime timestamp not null,
            create_id varchar(32) not null,
            constraint un_myuss_scheme_name unique (scheme_name));

INSERT INTO MT_SECURITY_SCHEME (SCHEME_ID, SCHEME_NAME, DESCRIPTION, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('4bd84c0f5486ade54a33e22320216644', 'System Property Module Administrator', 'Grants full permissions to manage MTS/CAT properties.', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME (SCHEME_ID, SCHEME_NAME, DESCRIPTION, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('c1c4d263be34a17fef70965b3e65b8d2', 'MTS Security Module Administrator', 'Grants full permissions to manage MTS user accounts.', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME (SCHEME_ID, SCHEME_NAME, DESCRIPTION, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('a521aea0b58bc1a6b56bd1f1c0d8c5f9', 'CAT Application Profile', 'Grants the permissions necessary to the CIR Administration Tool application user to fulfill its internal operations.', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'sdn');
INSERT INTO MT_SECURITY_SCHEME (SCHEME_ID, SCHEME_NAME, DESCRIPTION, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('9271a031493bd4d428aa90a61fb7d7ca', 'Super User Profile', 'Grants full permissions to manage any CAT module.', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');

-- //@UNDO
-- SQL to undo the change goes here.

drop table mt_security_scheme;
