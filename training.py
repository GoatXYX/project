#coding=utf-8

import logging
import os.path
import sys
import multiprocessing
from gensim.models import Word2Vec
from gensim.models.word2vec import LineSentence
from gensim.models.word2vec import Text8Corpus
from gensim.test.utils import datapath

if __name__ == '__main__':
    program = os.path.basename(sys.argv[0])
    logger = logging.getLogger(program)
    logging.basicConfig(format='%(asctime)s: %(levelname)s: %(message)s')
    logging.root.setLevel(level=logging.INFO)
    logger.info("running %s" % ' '.join(sys.argv))
    if len(sys.argv) < 4:
         sys.exit(1)
    inp, outp1, outp2 = sys.argv[1:4]
    
    sentences=Text8Corpus(inp)
    model = Word2Vec(sentences, sg=1, min_count=1,iter=50, size=256, workers=multiprocessing.cpu_count())
    
    model.save(outp1)
    model.wv.save_word2vec_format(outp2, binary=False)
