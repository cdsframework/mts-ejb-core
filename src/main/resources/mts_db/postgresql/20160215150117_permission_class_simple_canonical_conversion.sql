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

-- // permission class simple canonical conversion
-- Migration SQL that makes the change goes here.

update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.AddressDTO' where PERMISSION_OBJECT = 'AddressDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.AdminDTO' where PERMISSION_OBJECT = 'AdminDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.alert.AlertJobDTO' where PERMISSION_OBJECT = 'AlertJobDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.alert.AlertJobQueueDTO' where PERMISSION_OBJECT = 'AlertJobQueueDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.AppDTO' where PERMISSION_OBJECT = 'AppDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.AppLogDTO' where PERMISSION_OBJECT = 'AppLogDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.patient.AsthmaReferralDTO' where PERMISSION_OBJECT = 'AsthmaReferralDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.AuditLogDTO' where PERMISSION_OBJECT = 'AuditLogDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.base.BaseDTO' where PERMISSION_OBJECT = 'BaseDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsBusinessScopeDTO' where PERMISSION_OBJECT = 'CdsBusinessScopeDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsCodeDTO' where PERMISSION_OBJECT = 'CdsCodeDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsCodeSystemDTO' where PERMISSION_OBJECT = 'CdsCodeSystemDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsConceptDTO' where PERMISSION_OBJECT = 'CdsConceptDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsDiseaseDTO' where PERMISSION_OBJECT = 'CdsDiseaseDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsDiseaseSeriesDTO' where PERMISSION_OBJECT = 'CdsDiseaseSeriesDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsListDTO' where PERMISSION_OBJECT = 'CdsListDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsVaccineCompositionDTO' where PERMISSION_OBJECT = 'CdsVaccineCompositionDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsVaccineDTO' where PERMISSION_OBJECT = 'CdsVaccineDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsVaccineGroupDTO' where PERMISSION_OBJECT = 'CdsVaccineGroupDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CdsVersionDTO' where PERMISSION_OBJECT = 'CdsVersionDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.ConceptDeterminationMethodDTO' where PERMISSION_OBJECT = 'ConceptDeterminationMethodDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.ConditionDTO' where PERMISSION_OBJECT = 'ConditionDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.CriteriaDTO' where PERMISSION_OBJECT = 'CriteriaDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.DataModelDTO' where PERMISSION_OBJECT = 'DataModelDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.DataTemplateDTO' where PERMISSION_OBJECT = 'DataTemplateDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.encounter.EncounterReviewDTO' where PERMISSION_OBJECT = 'EncounterReviewDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.facility.FacilityDTO' where PERMISSION_OBJECT = 'FacilityDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.facility.FacilityGroupDTO' where PERMISSION_OBJECT = 'FacilityGroupDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.FileDTO' where PERMISSION_OBJECT = 'FileDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.hl7.Hl7AccountDTO' where PERMISSION_OBJECT = 'Hl7AccountDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.hl7.Hl7ErrorStatsDTO' where PERMISSION_OBJECT = 'Hl7ErrorStatsDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.hl7.Hl7MessageDTO' where PERMISSION_OBJECT = 'Hl7MessageDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.hl7.Hl7MessageErrorDTO' where PERMISSION_OBJECT = 'Hl7MessageErrorDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.hl7.Hl7MessageLogDTO' where PERMISSION_OBJECT = 'Hl7MessageLogDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.hl7.Hl7MessageStatsDTO' where PERMISSION_OBJECT = 'Hl7MessageStatsDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceSeasonDTO' where PERMISSION_OBJECT = 'IceSeasonDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceSeriesDTO' where PERMISSION_OBJECT = 'IceSeriesDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceTestDTO' where PERMISSION_OBJECT = 'IceTestDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceTestGroupDTO' where PERMISSION_OBJECT = 'IceTestGroupDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceTestGroupTestDTO' where PERMISSION_OBJECT = 'IceTestGroupTestDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceTestResultDTO' where PERMISSION_OBJECT = 'IceTestResultDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceTestSuiteDTO' where PERMISSION_OBJECT = 'IceTestSuiteDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceVaccineComponentDTO' where PERMISSION_OBJECT = 'IceVaccineComponentDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceVaccineDTO' where PERMISSION_OBJECT = 'IceVaccineDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.IceVaccineGroupDTO' where PERMISSION_OBJECT = 'IceVaccineGroupDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.JurisdictionDTO' where PERMISSION_OBJECT = 'JurisdictionDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.LogicSetDTO' where PERMISSION_OBJECT = 'LogicSetDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.OpenCdsConceptDTO' where PERMISSION_OBJECT = 'OpenCdsConceptDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.patient.PatientDTO' where PERMISSION_OBJECT = 'PatientDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.encounter.PatientEncounterDTO' where PERMISSION_OBJECT = 'PatientEncounterDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.pqr.PqrDataSourceDTO' where PERMISSION_OBJECT = 'PqrDataSourceDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.provider.ProviderDTO' where PERMISSION_OBJECT = 'ProviderDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.RckmsUserDTO' where PERMISSION_OBJECT = 'RckmsUserDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.ReporterTypeDTO' where PERMISSION_OBJECT = 'ReporterTypeDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.SecuritySchemeDTO' where PERMISSION_OBJECT = 'SecuritySchemeDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.SessionDTO' where PERMISSION_OBJECT = 'SessionDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.SystemPropertyDTO' where PERMISSION_OBJECT = 'SystemPropertyDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.cir.TcUserDTO' where PERMISSION_OBJECT = 'TcUserDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.UserDTO' where PERMISSION_OBJECT = 'UserDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.ValueSetDTO' where PERMISSION_OBJECT = 'ValueSetDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.webutd.WebUtdJobParameterDTO' where PERMISSION_OBJECT = 'WebUtdJobParameterDTO';
update MT_SECURITY_SCHEME_PERM_MAP set PERMISSION_OBJECT = 'org.cdsframework.dto.' where PERMISSION_OBJECT = '';

delete from MT_SECURITY_SCHEME_PERM_MAP where PERMISSION_OBJECT in ('UserAddressDTO','UserEmailDTO','UserPhoneDTO','AlertAttachmentDTO','AlertDTO','AlertLetterDTO','AlertReportDTO','AlertReportRequestDTO','AlertReportRequestQueueDTO');

-- //@UNDO
-- SQL to undo the change goes here.


