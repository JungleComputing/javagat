#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import cgi
import datetime
import logging

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from django.utils import simplejson 

##Data types (Models)

class Advert(db.Model):
  path   = db.StringProperty()
  author = db.UserProperty()
  ttl    = db.DateTimeProperty(auto_now_add=True)
  object = db.TextProperty() #base64
  
class MetaData(db.Model):
  path   = db.StringProperty()
  keystr = db.StringProperty()
  value  = db.StringProperty()

##Private Functions

#Authentication
def auth(self): 
  if not users.get_current_user():
    self.error(403)
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('Not Authenticated')
    return -1

  if not users.is_current_user_admin():
    self.error(403)
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('No Administrator')
    return -1

  return 0

#Storing a JSON object 
def store(json):
  advert = Advert()
  user   = users.get_current_user()
  
  advert.path   = json[0] #extract path from message
  advert.author = user    #store author
  advert.object = json[2] #extract (base64) object from message

  advert.put() #store object in database

  for k in json[1].keys():
    metadata        = MetaData(parent=advert)
    metadata.path   = json[0]
    metadata.keystr = k
    metadata.value  = json[1][k]
    metadata.put() #store metadata
  
  return
  
#Deleting an Object including MetaData
def remove(path):
  query1 = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", path)
  query2 = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1", path)
  
  keys = []
  
  for q1 in query1:
    keys.append(q1.key())
  
  for q2 in query2:
    keys.append(q2.key())
    
  try: #transaction remove
    db.run_in_transaction(db.delete, keys)
  except db.TransactionFailedError, message:
    logging.error(message) #log the error
    self.error(503)        #send response to client
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write(message)
    return -1
  
  return 0 #successful remove

#Garbage Collector
def gc(): 
  query = db.GqlQuery("SELECT * FROM Advert WHERE ttl < :1", datetime.datetime.today() + datetime.timedelta(days=-10))
  
  for advert in query: #all entities that can be deleted
    advert.delmd()     #delete all associated metadata
    advert.delete()    #delete the object itself

##Public Functions

class MainPage(webapp.RequestHandler):
  def get(self):
    self.redirect(users.create_login_url(self.request.uri))

class AddObject(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    response = 201 #standard response
    body = self.request.body
    json = simplejson.loads(body)
    
    query = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", json[0])
    if query.count() > 0: #this entry already exists; overwrite
      if remove(json[0]) is 0: #remove went well
        response = 205 #reset content

    try: #try storing the JSON object (in transaction)
      db.run_in_transaction(store, json) 
    except db.TransactionFailedError, message:
      logging.error(message) #log error message
      gc()                   #run garbage collector
      
      try: #second try
        db.run_in_transaction(store, json)
      except db.TransactionFailedError, message:
        logging.error(message) #log the error
        self.error(503)        #send response to client
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write(message)
        return
    
    self.response.http_status_message(response) #created/overwritten
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('Expires: ') 
    self.response.out.write(datetime.datetime.today() + datetime.timedelta(days=10)) 
    return

class DelObject(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body  = self.request.body
    query1 = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", body)
    query2 = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1", body)
    
    if query1.count() < 1: #no matching object found
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('No Such Element')
      return      
    
    if remove(body) is 0: #remove went well
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('OK')
    
class GetObject(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body  = self.request.body
    query = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", body)
    
    if query.count() < 1: #no matching object found
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('No Such Element')
      return      
    
    for advert in query:
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write(advert.object) #returning the first entry we find
      break #and stop
    
    return;
  
class GetMetaData(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body  = self.request.body
    query = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1", body)
    
    if query.count() < 1: #no matching object found
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('No Such Element')
      return
    
    jsonObject = {}
    
    for metadata in query:
      jsonObject[metadata.keystr] = metadata.value
      
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write(simplejson.dumps(jsonObject))   

class FindMetaData(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body = self.request.body
    json = simplejson.loads(body)
    
    query = db.GqlQuery("SELECT * FROM MetaData")
    
    paths = Set()
    
    for bin in query:
      paths.add(bin.path)
      
    paths  = list(paths)
    self.response.out.write(paths)
    
    for path in paths[:]:
      for k in json.keys():
        query = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1 AND keystr = :2 AND val = :3", path, k, json[k])
        if query.count() < 1:
          paths.remove(path)
          break
    
    if len(paths) < 1:
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('Not Found')
      return
    
    self.response.out.write(simplejson.dumps(paths))  

application = webapp.WSGIApplication(
                                     [('/',      MainPage),
                                      ('/add',   AddObject),
                                      ('/del',   DelObject),
                                      ('/get',   GetObject),
                                      ('/getmd', GetMetaData),
                                      ('/find',  FindMetaData)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()