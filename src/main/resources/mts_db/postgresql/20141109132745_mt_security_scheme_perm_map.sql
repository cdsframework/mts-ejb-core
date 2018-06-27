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

-- // mt_security_scheme_perm_map
-- Migration SQL that makes the change goes here.

create table mt_security_scheme_perm_map (
            map_id varchar(32) primary key,
            scheme_id varchar(32) not null,
            permission_object varchar(260) not null,
            permission_type varchar(1) not null,
            cascade_perm VARCHAR(1) default 'Y' not null,
            deny VARCHAR(1) default 'N' not null,
            last_mod_datetime timestamp not null,
            last_mod_id varchar(32) not null,
            create_datetime timestamp not null,
            create_id varchar(32) not null,
            constraint fk_mt_sspm_to_mt_ss foreign key (scheme_id) references mt_security_scheme (scheme_id),
            constraint un_mt_sspm_sid_po_pt unique (scheme_id, permission_object, permission_type));

INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('e2f96dfca8a3a4d0edb81514aba06956', '4bd84c0f5486ade54a33e22320216644', 'SystemPropertyDTO', 'F', 'Y', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('68a5a44a1e172a287a59bcce20f9b5dd', '9271a031493bd4d428aa90a61fb7d7ca', 'BaseDTO', 'F', 'Y', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('548b53ae8095e74b6acbe043d99e62d2', 'a521aea0b58bc1a6b56bd1f1c0d8c5f9', 'BaseDTO', 'S', 'N', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('90dd8757984c124c7b7c9540c787ae50', 'a521aea0b58bc1a6b56bd1f1c0d8c5f9', 'UserDTO', 'U', 'Y', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('653c5950ab11e274929a41fbb5c1ff86', 'c1c4d263be34a17fef70965b3e65b8d2', 'AppDTO', 'F', 'Y', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('8c97e9f65ac3f8122d43b324403a1846', 'c1c4d263be34a17fef70965b3e65b8d2', 'UserDTO', 'F', 'Y', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('e2cc85f95e733708be17a8f1b584654a', 'c1c4d263be34a17fef70965b3e65b8d2', 'SecuritySchemeDTO', 'F', 'Y', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');
INSERT INTO MT_SECURITY_SCHEME_PERM_MAP (MAP_ID, SCHEME_ID, PERMISSION_OBJECT, PERMISSION_TYPE, CASCADE_PERM, DENY, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID) 
    VALUES ('649ff249678a4adcc3be2d8fe464654c', 'c1c4d263be34a17fef70965b3e65b8d2', 'SessionDTO', 'F', 'Y', 'N', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e');

-- //@UNDO
-- SQL to undo the change goes here.

drop table mt_security_scheme_perm_map;
