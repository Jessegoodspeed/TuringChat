## Chat Model

This folder contains key files used for creating the message dataset and training the chat model. The implemented Python scripts (Seq2Seq, Word2Vec, and createDataset) are originally from Adit Deshpande's [repo](https://github.com/adeshpande3/Facebook-Messenger-Bot).
The _models2/_ directory is an experimental model that used 500 word vectors, while the chat model in the *TuringChat* backend was trained with 300 word vectors. _models2_ has yet to be tested for comparison.
#### Training on your own data
I recommend [FloydHub](https://www.floydhub.com/) for training the chat model. To extend this version with your own data, I suggest using both the included Seq2SeqXTrain.py and Seq2SeqYTrain.py when you run Seq2Seq.py so that it progresses from previous training as opposed to starting from scratch. These two files hold the learned parameters from previous training epochs. Note that on line 152 of Seq2Seq.py, "wordVecDimensions = 500".
