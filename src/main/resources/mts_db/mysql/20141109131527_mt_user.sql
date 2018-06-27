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

-- // mt_user
-- Migration SQL that makes the change goes here.

create table mt_user (
        user_id varchar(32) primary key,
        username varchar(64) not null,
        prefix varchar(40),
        first_name varchar(40) not null,
        middle_name varchar(40),
        last_name varchar(40) not null,
        suffix varchar(40),
        failed_login_attempts integer,
        disabled varchar(1),
        expiration_date date,
        change_password varchar(1),
        password_hash varchar(64),
        app_proxy_user varchar(1),
        proxy_app_id varchar(32),
        email varchar(320) not null,
        last_mod_datetime timestamp not null,
        last_mod_id varchar(32) not null,
        create_datetime timestamp not null,
        create_id varchar(32) not null,
        constraint fk_mt_user_to_mt_app foreign key (proxy_app_id) references mt_app (app_id),
        constraint un_mt_user_username unique (username));

INSERT INTO MT_USER (USER_ID, USERNAME, FAILED_LOGIN_ATTEMPTS, DISABLED, EXPIRATION_DATE, CHANGE_PASSWORD, PASSWORD_HASH, APP_PROXY_USER, PROXY_APP_ID, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID, PREFIX, FIRST_NAME, MIDDLE_NAME, LAST_NAME, SUFFIX, EMAIL) 
    VALUES ('4326780713b8f2513dd63826d78b298b', 'cat', 0, 'N', NULL, 'N', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'Y', 'cad52339d32c7ef4ecfc66cf4ddef00c', CURRENT_TIMESTAMP, '9b198d5b4982ae690a32b6cfdac8a130', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', NULL, 'Cat', NULL, 'Administrator', NULL, 'scm@cdsframework.org');

INSERT INTO MT_USER (USER_ID, USERNAME, FAILED_LOGIN_ATTEMPTS, DISABLED, EXPIRATION_DATE, CHANGE_PASSWORD, PASSWORD_HASH, APP_PROXY_USER, PROXY_APP_ID, CREATE_DATETIME, CREATE_ID, LAST_MOD_DATETIME, LAST_MOD_ID, PREFIX, FIRST_NAME, MIDDLE_NAME, LAST_NAME, SUFFIX, EMAIL) 
    VALUES ('ddce5b65bceb5d26a449e9076d31da9d', 'admin', 0, 'N', NULL, 'N', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'N', NULL, CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', CURRENT_TIMESTAMP, 'a587fc915db8c00b2c7f8141dbc5c16e', NULL, 'Default', 'Admin', 'User', NULL, 'scm@cdsframework.org');

-- //@UNDO
-- SQL to undo the change goes here.

drop table mt_user;
