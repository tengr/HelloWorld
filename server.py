#!/usr/bin/python
import os, sys, json, xmpp, random, string

SERVER = 'gcm.googleapis.com'
PORT = 5235
#USERNAME = "Your GCM Sender Id"
USERNAME = "22601053657"
PASSWORD = "AIzaSyAyeqel0T5wicjpNIWzZ9s1yDSytZZNwHM"
REGISTRATION_ID = "Registration Id of the target device"

unacked_messages_quota = sys.maxint
send_queue = []
lats = []
longs = []
moving = True;
# Return a random alphanumerical id
def random_id():
  rid = ''
  for x in range(8): rid += random.choice(string.ascii_letters + string.digits)
  return rid

def message_callback(session, message):
  global unacked_messages_quota
  gcm = message.getTags('gcm')
  if gcm:
    gcm_json = gcm[0].getData()
    msg = json.loads(gcm_json)
    print "msg is "
    print msg
    if not msg.has_key('message_type'):
      if msg.has_key('data'):
        lats.append(float(msg['data']['latitude']))
        longs.append(float(msg['data']['longtitude']))
        print 'latitude:' + msg['data']['latitude'] + "\tlongtitude" + msg['data']['longtitude']
        if len(lats) > 1 and (lats[-1] - lats[-2]) ** 2 + (longs[-1] - longs[-1]) ** 2 < 0.0001:
          moving = False
          print 'Not Moving!'
        else:
          moving = True
      # Acknowledge the incoming message immediately.
      send({'to': msg['from'],
            'message_type': 'ack',
            'message_id': msg['message_id']})
      print "sent ack"
      # Queue a response back to the server.
      if msg.has_key('from'):
        # Send a dummy echo response back to the app that sent the upstream message.
        if moving:
          send_queue.append({'to': msg['from'],
                           'message_id': random_id(),
                           'data': {'moving': "True"}})
        else: 
          send_queue.append({'to': msg['from'],
                           'message_id': random_id(),
                           'data': {'latitude': "-37.796369",
                                    'longtitude': "144.961174"
                                    }
                            })
      else:
        print "Message missing key \"from\""
    elif msg['message_type'] == 'ack' or msg['message_type'] == 'nack':
      unacked_messages_quota += 1
      print "message_type received"
      print msg['message_type']
  else:
    print "GCM Problem!"

def send(json_dict):
  template = ("<message><gcm xmlns='google:mobile:data'>{1}</gcm></message>")
  client.send(xmpp.protocol.Message(
      node=template.format(client.Bind.bound[0], json.dumps(json_dict))))

def flush_queued_messages():
  global unacked_messages_quota
  while len(send_queue) and unacked_messages_quota > 0:
    send(send_queue.pop(0))
    unacked_messages_quota -= 1

client = xmpp.Client('gcm.googleapis.com', debug=['socket'])
client.connect(server=(SERVER,PORT), secure=1, use_srv=False)
auth = client.auth(USERNAME, PASSWORD)
if not auth:
  print 'Authentication failed!'
  sys.exit(1)

client.RegisterHandler('message', message_callback)

send_queue.append({'to': REGISTRATION_ID,
                   'message_id': 'reg_id',
                   'data': {'message_destination': 'RegId',
                            'message_id': random_id()}})

while True:
  client.Process(1)
  flush_queued_messages()