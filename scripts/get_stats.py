import json
import argparse
import collections
import os
import sys
import numpy as np

from labels import *
from plotting import heatmap_plot as hp

class Statistic:

	def __init__(self, babel_report_path, interfaces_folder_path):
		self._babel_report = babel_report_path # path to folder containing babelview reports
		self.interfaces_folder = interfaces_folder_path
		self.all_ids = set()
		self.vuln_ids = set()
		self._load_app_ids()
	
	def _load_app_ids(self):
		for apk in os.listdir(self._babel_report):
			apk_id = apk.split('.')[0]
			self.all_ids.add(apk_id)
			if self._is_vulnerable(apk_id):
				self.vuln_ids.add(apk_id)

	def _check_intent_control(self, i_alarm):
		
		if 'IMAGE_CAPTURE' in i_alarm:
			return TAKE_PICT
		elif 'CALL' in i_alarm:
			return CALL
		elif 'SEND_MULTIPLE' in i_alarm or 'SENDTO' in i_alarm or 'SEND' in i_alarm:
			return EMAIL_SMS		
		elif 'getLaunchIntentForPackage' in i_alarm:
			return NEW_APP
		elif 'DIAL' in i_alarm:
			return DIAL
		elif 'INSERT' in i_alarm or 'EDIT' in i_alarm:
			return CALENDAR
		elif '*' == i_alarm:
			return UKNOWN_INTENT


	def _intent_control(self, res_list):
		ivulns = set()
		for i_alarm in res_list:
			final_alarm = self._check_intent_control(i_alarm)
			if i_alarm != 'FP' and 'VIEW' not in i_alarm and final_alarm is not None:
				ivulns.add( final_alarm )
		
		return ivulns
	
	def _vuln_in_report(self, report_path):
		with open(report_path, 'r') as rep_file:
			json_rep = json.loads(rep_file.read())

		results = set()
		for alarm in json_rep:
			if isinstance(json_rep[alarm], list):
				intent_control_vuln = self._intent_control(json_rep[alarm])
				if intent_control_vuln:
					results.update(intent_control_vuln)
			elif json_rep[alarm] == 1:
				results.add(alarm) 
		
		return results

	def _check_iface_name(self, iface_name):
		if 'createCalendarEvent' == iface_name:
			return CALENDAR
		elif 'playAudio' == iface_name or 'playVideo' == iface_name:
			return PLAY
		elif 'postToSocial' == iface_name:
			return SOCIAL
		elif 'storePicture' == iface_name or 'Download' in iface_name:
			return DOWNLOAD
		elif 'send' == iface_name or 'sendMail' == iface_name or 'sms' == iface_name or 'sendSMS' == iface_name:
			return EMAIL_SMS
		elif 'makeCall' == iface_name:
			return DIAL
		elif 'captureImage' == iface_name:
			return TAKE_PICT
		elif 'saveContent' == iface_name:
			return None
		else:
			return None

	def _vuln_in_iface_name(self, apk_id):
		ifaceFold = os.path.join(self.interfaces_folder, apk_id)
		iface_name_vulns = set()

		
		if os.path.isdir(ifaceFold):
			for iface_path in os.listdir(ifaceFold):
				iface_name = iface_path.split('.json')[0]
				vuln = self._check_iface_name(iface_name)
				if vuln is not None: 
					iface_name_vulns.add(vuln)
		
		return iface_name_vulns
		

	def _is_vulnerable(self, apk_id):
		report = os.path.join(self._babel_report, apk_id+'.json')
		# we first check in babelView report to see if
		return self._vuln_in_report(report) or self._vuln_in_iface_name(apk_id)


	def __generate_correlation_matrix(self, apk_dict):
		# Rows: vulnerabilities
		# Columns: apks
		matrix = [[0 for x in range(len(apk_dict.keys()))] for y in range(len(labels))]

		row = 0
		col = 0
		for apk_id in apk_dict:
			for vuln in labels:
				value = apk_dict[apk_id][vuln]
				matrix[row][col] = value
				row += 1 
			row = 0
			col+=1
		return np.corrcoef(matrix)

	def plot_correlation_map(self, vulnerable_id_path):
		with open(vulnerable_id_path, 'r') as f:
			apk_ids = f.readlines()

		# Generate a dictionary mapping {APK -> {vuln: int}}
		apk_dict = dict()
		for apk_id in apk_ids:
			apk_id = apk_id.strip()
			apk_dict[apk_id] = dict()
			report_path = os.path.join(self._babel_report, apk_id+'.json')
			vuln_report = self._vuln_in_report(report_path)
			ifacename_vuln = self._vuln_in_iface_name(apk_id)

			#print (ifacename_vuln)
			# Extracting vulns for this APK
			# Since we are using vulnerables id path, these are only
			# sanity cheks
			vuln_set = set()
			if ifacename_vuln and vuln_report:
				vuln_set = vuln_report.union(ifacename_vuln)
			elif ifacename_vuln:
				vuln_set = ifacename_vuln
			elif vuln_report:
				vuln_set = vuln_report

			for vuln in labels:
				if vuln in vuln_set:
					apk_dict[apk_id][vuln] = 1
				else:
					apk_dict[apk_id][vuln] = 0

		m = self.__generate_correlation_matrix(apk_dict)
		hp.corr_plot(m, labels)


	def generate_vulnerable_id(self):
		f = open('vulnerable.txt', 'a')
		for apk in os.listdir(self._babel_report):
			apk_id = apk.split('.')[0]
			if self._is_vulnerable(apk_id):
				f.write(apk_id+'\n')
		f.close()

	def extract_vulnerabilities(self):
		results = dict()
		for l in labels:
			results[l] = 0

		tot_vuln = 0
		for apk in os.listdir(self._babel_report):
			apk_id = apk.split('.')[0]
			r_path = os.path.join(self._babel_report, apk_id+'.json')
			
			report_vulns = self._vuln_in_report(r_path)
			iface_name_vulns = self._vuln_in_iface_name(apk_id)
			
			vulns = set()
			if iface_name_vulns and report_vulns:
				vulns = report_vulns.union(iface_name_vulns)
			elif iface_name_vulns:
				vulns = iface_name_vulns
			elif report_vulns:
				vulns = report_vulns
			
			
			for alarm in vulns:
				results[alarm] += 1
				tot_vuln+=1

		print(results)
		print(tot_vuln)

	def get_vulnerable_to_ids(self, vuln):
		id_found = set()
		for apk_id in self.vuln_ids:
			r_path = os.path.join(self._babel_report, apk_id+'.json')
			
			report_vulns = self._vuln_in_report(r_path)
			iface_name_vulns = self._vuln_in_iface_name(apk_id)
			
			vulns = set()
			if iface_name_vulns and report_vulns:
				vulns = report_vulns.union(iface_name_vulns)
			elif iface_name_vulns:
				vulns = iface_name_vulns
			elif report_vulns:
				vulns = report_vulns

			if vuln in vulns:
				id_found.add(apk_id)

		for apk_id in id_found:
			print(apk_id)

	def vuln_to(self, apk_id):
		r_path = os.path.join(self._babel_report, apk_id+'.json')
		report_vulns = self._vuln_in_report(r_path)
		iface_name_vulns = self._vuln_in_iface_name(apk_id)
		
		vulns = set()
		if iface_name_vulns and report_vulns:
			vulns = report_vulns.union(iface_name_vulns)
		elif iface_name_vulns:
			vulns = iface_name_vulns
		elif report_vulns:
			vulns = report_vulns

		print(vulns)
	
	def app_per_library(self, library):
		tot_apks = 0
		for apk_id in self.vuln_ids:
			lib_folder = os.path.join('../allLib', apk_id)

			if os.path.isdir(lib_folder):
				used = 0
				for lib in os.listdir(lib_folder):
					with open(os.path.join(lib_folder, lib), 'r') as jf:
						j = json.loads(jf.read())
					library_used = j['library']
					if library in library_used:
						used = 1
						break
				tot_apks += used

		print(tot_apks)



def main():

	parser = argparse.ArgumentParser(description='BabelView post analysis script')
	
	parser.add_argument('reports', metavar='rep', type=str, nargs=1,
						help='path to babelview report folder')
	
	parser.add_argument('interfaces', metavar='ifaces', type=str, nargs='?', default='interfaces',
						help='Path to interfaces folder')
	
	args = parser.parse_args()
	
	s = Statistic(args.reports[0], args.interfaces)

	
	s.extract_vulnerabilities()
	

if __name__ == '__main__':
	main()

	

'''
Frame Confusion: 1039
Take Picture: 7
Send Email or SMS Intent: 586
Reflection: 1030
Method Parameter: 622
TM Leaks: 39
Fetch Class: 25
Post to Social: 293
Call via Intent: 314
File Reading: 593
Pref Connectivity Leaks: 3
Start New App: 1321
SQL-lite Leaks: 136
Pref TM Leak: 4
Connectivity Leaks: 0
Calendare Interaction: 357
Fetch method: 85
Open Socket: 0
SQL-lite Query Exec: 438
Pref Location Leaks: 1
Direct Call: 19
Play Video or Audio: 687
Pref DB Leak: 11
File Writing: 1444
Preferences DB Query Exec: 0
Location Leaks: 43
Constructor Instance: 13
Send SMS: 6
Save Download Picture: 319
File Opening: 385
Fetch Constructor: 0
'''