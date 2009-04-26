all: lcoretest

nomessages:
	fpc -Sd -gl -dipv6 -dnomessages lcoretest.dpr

lcoretest: *.pas *.inc lcoretest.dpr
	fpc -Sd -gl -dipv6 lcoretest.dpr
	
clean:
	-rm *.o
	-rm *.ppu
	-rm *.exe
	-rm *.dcu
	-rm lcoretest

date := $(shell date +%Y%m%d)

zip:
	mkdir -p lcorewin32_$(date)
	cp -a *.pas lcorewin32_$(date)
	cp -a *.inc lcorewin32_$(date)
	cp -a *.dpr lcorewin32_$(date)
	cp -a Makefile lcorewin32_$(date)
	-rm ../lcorewin32_$(date).zip
	zip -r ../lcorewin32_$(date).zip lcorewin32_$(date)
	rm -rf lcorewin32_$(date)