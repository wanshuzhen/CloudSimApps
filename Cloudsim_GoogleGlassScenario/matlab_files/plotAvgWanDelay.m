function [] = plotAvgWanDelay()
    filePath = getCloudSimConf(1);
    simTime = getCloudSimConf(2);
    numOfSimulations = getCloudSimConf(3);
    stepOfxAxis = getCloudSimConf(4);
    vmType = getCloudSimConf(5);
    startOfMobileDeviceLoop = getCloudSimConf(10);
    stepOfMobileDeviceLoop = getCloudSimConf(11);
    endOfMobileDeviceLoop = getCloudSimConf(12);
    numOfMobileDevices = (endOfMobileDeviceLoop - startOfMobileDeviceLoop)/stepOfMobileDeviceLoop + 1;

     results = zeros(1,numOfMobileDevices);
     for s=1:numOfSimulations
         for j=startOfMobileDeviceLoop:stepOfMobileDeviceLoop:endOfMobileDeviceLoop
             filePathCreated = strcat(filePath,'SIMRESULT_ITE',int2str(s),'_CLOUD_WIFI_CLIENT_MEAN20_',int2str(j),'DEVICE_SUCCESS.log');
             readData = dlmread(filePathCreated,';',1,0);

             results(1,j) = results(1,j) + (mean(readData(:,5)));
         end
     end
     results = results/numOfSimulations;
  
    types = zeros(1,numOfMobileDevices);
    for i=1:numOfMobileDevices
        types(i)=i*stepOfMobileDeviceLoop;
    end
    
    hFig = figure;
    set(hFig, 'Position',[100 200 300 300]);
    if(getCloudSimConf(20) == 1)
        for i=stepOfxAxis:stepOfxAxis:endOfMobileDeviceLoop
            plot(i, results(1,i),':ko','MarkerFaceColor',getCloudSimConf(21),'color',getCloudSimConf(21));
            hold on;
        end
        plot(types, results(1,:),':k','color',getCloudSimConf(21),'LineWidth',1.5);
        set(gca,'color','none');
    else
        plot(types, results(1,:),'-kv','MarkerFaceColor','w','LineWidth',1.4);
        set(gcf, 'Position',getCloudSimConf(28));
    end
    lgnd = legend('ADSL-WAN','Location','NorthWest');
    if(getCloudSimConf(20) == 1)
        set(lgnd,'color','none');
    end
    
    hold off;
    axis square
    xlabel(getCloudSimConf(9));
    set(gca,'XTick', stepOfxAxis:stepOfxAxis:numOfMobileDevices*stepOfxAxis);
    ylabel('Network Delay (seconds)');
	%set(gca,'YLim',[2 6]);
end