function [] = plotAvgTaskDelay()
    filePath = getCloudSimConf(1);
    simTime = getCloudSimConf(2);
    numOfSimulations = getCloudSimConf(3);
    stepOfxAxis = getCloudSimConf(4);
    vmType = getCloudSimConf(5);
    startOfMobileDeviceLoop = getCloudSimConf(10);
    stepOfMobileDeviceLoop = getCloudSimConf(11);
    endOfMobileDeviceLoop = getCloudSimConf(12);
    numOfMobileDevices = (endOfMobileDeviceLoop - startOfMobileDeviceLoop)/stepOfMobileDeviceLoop + 1;

     results = zeros(size(vmType,2),numOfMobileDevices);
     for s=1:numOfSimulations
         for i=1:size(vmType,2)
             for j=1:numOfMobileDevices
                 filePathCreated = strcat(filePath,'SIMRESULT_ITE',int2str(s),'_',char(vmType(i)),'_MEAN20_',int2str(j),'DEVICE_SUCCESS.log');
                 readData = dlmread(filePathCreated,';',1,0);

                 results(i,j) = results(i,j) + (mean(readData(:,5)));
             end
         end
     end
     results = results/numOfSimulations;
  
    types = zeros(1,numOfMobileDevices);
    for i=1:numOfMobileDevices
        types(i)=i*stepOfMobileDeviceLoop;
    end
    
    hFig = figure;
    set(hFig, 'Position',getCloudSimConf(7));
    if(getCloudSimConf(20) == 1)
        for i=stepOfxAxis:stepOfxAxis:endOfMobileDeviceLoop
            plot(i, results(1,i),':k*','MarkerFaceColor',getCloudSimConf(21),'color',getCloudSimConf(21));
            hold on;
            plot(i, results(2,i),':ko','MarkerFaceColor',getCloudSimConf(22),'color',getCloudSimConf(22));
            hold on;
            %plot(i, results(3,i),':ks','MarkerFaceColor',getCloudSimConf(23),'color',getCloudSimConf(23));
            %hold on;
        end
        
        plot(types, results(1,:),':k','color',getCloudSimConf(21),'LineWidth',1.5);
        hold on;
        plot(types, results(2,:),':k','color',getCloudSimConf(22),'LineWidth',1.5);
        hold on;
        %plot(types, results(3,:),':k','LineWidth',1.5);
        %hold on;
    
        set(gca,'color','none');
    else
        plot(types, results(1,:),'-kv','MarkerFaceColor','w','LineWidth',1.4);
        hold on;
        plot(types, results(2,:),'-ks','MarkerFaceColor','w','LineWidth',1.4);
        %hold on;
        %plot(types, results(3,:),'-ko','MarkerFaceColor','w','LineWidth',1.4);
        
        set(gcf, 'Position',getCloudSimConf(28));
    end
    lgnd = legend(getCloudSimConf(6),'Location','NorthWest');
    if(getCloudSimConf(20) == 1)
        set(lgnd,'color','none');
    end
    
    hold off;
    axis square
    xlabel('Number of Mobile Device');
    set(gca,'XTick', stepOfxAxis:stepOfxAxis:numOfMobileDevices*stepOfxAxis);
    ylabel('Average Network Delay');
	%set(gca,'YLim',[2 6]);
end