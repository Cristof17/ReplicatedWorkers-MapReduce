Cristofor Rotsching 333CA

Foreach filename that was contained in the input file I created an 
instance of Documen class which contains the name of the file, and 
the fragment size for that specific file. The document is splitted into
fragments using "split()" method.This returns an array of PartialTexts,
those being tasks for Map. Every Map result returns an instance of class
MapResult which contains the hash, the list of maximum words and the length
of each maximum word. Each MapResult is mapped into a hashTable as a value,
and the key is the fragmentID(the number of the document in the input file).
The HashMap containing every MapResult from all the files is being populated 
via a callback method, defined by the interfaces : MapResultFinishedCallback,
ReduceResultFinishedCallback.
After join has been called for the MapWorkers, the ResultWorkers read
the MapResults HashMap and then combine each List<MapResult> from keys representing
document postition to form the master MapResult object containing the combined MapResults
of the List<MapResult>. I chose to save the master MapResult as the first MapResult object
of the List<MapResult>. 

For parsing the input file I have done it in the following manner.

Postition the filePointer at the beginning of the fragment, and the check if the 
filePointer is in the middle of the word. If it is in the middle , I call readByte()
method of RandomAccessFile object "raf" to ski the current byte. I do this as long as 
the current byte is different from a delimiter.
After skipping the necessary bytes, I start to count how many characters are there until
the next delimiter. I do this because I must use the readFully(buffer, start ,length) method
of RandomAccessFile object, otherwise the caracters reprezented with values greater than 127 will
not be taken into account.


Scalability test:
(The test has been done locally on my machine)
Intel(R) Core(TM) i7-3537U CPU @ 2.00GHz
2 cores with hyper-threading

test1.txt
	1 Thread: 2.9998531 seconds
	2 Thread: 1.8498939 seconds
	4 Thread: 1.8259237 seconds

test2.txt
	1 Thread: 25.627882 seconds
	2 Thread: 17.094595 seconds
	4 Thread: 12.517339 seconds

test3.txt
	1 Thread: 14.511555 seconds
	2 Thread: 9.275021 seconds
	4 Thread: 6.681061 seconds

test4.txt
	1 Thread: 93.150720 seconds
	2 Thread: 55.761986 seconds
	4 Thread: 41.295574 seconds

