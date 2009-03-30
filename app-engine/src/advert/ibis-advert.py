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

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from django.utils import simplejson 

class Advert(db.Model):
  path   = db.StringProperty()
  author = db.UserProperty()
  ttl    = db.DateTimeProperty(auto_now_add=True)
  object = db.BlobProperty()
  
class MetaData(db.Model):
  keystr = db.StringProperty()
  value  = db.StringProperty()

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

class MainPage(webapp.RequestHandler):
  def get(self):
    self.redirect(users.create_login_url(self.request.uri))

class AddObject(webapp.RequestHandler):
  def post(self):
    advert = Advert()
    user   = users.get_current_user()
    
    if auth(self) < 0: return
    
    body = self.request.body
    json = simplejson.loads(body)
    
    advert.path   = json[0] #extract path from message
    advert.author = user    #store author
    advert.object = json[2] #extract (base64) object from message
    
    advert.put() #store object in database
    
    for k in json[1].keys():
      metadata        = MetaData(parent=advert)
      metadata.keystr = k
      metadata.value  = json[1][k]
      metadata.put()
    
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('TTL') #TODO: insert actual TTL
    return

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