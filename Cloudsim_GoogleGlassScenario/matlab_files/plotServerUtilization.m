function [] = plotServerUtilization()
    filePath = getCloudSimConf(1);
    simTime = getCloudSimConf(2);
    serverLoadLogInterval = getCloudSimConf(8);
    numOfSimulations = getCloudSimConf(3);
    vmType = getCloudSimConf(5);
    brokerNum = 47;
    numberOfLines=simTime/serverLoadLogInterval-1;

     results = zeros(size(vmType,2),numberOfLines);
     for s=1:numOfSimulations
         for i=1:size(vmType,2)
             %for j=1:numOfMobileDevices
                 filePathCreated = strcat(filePath,'SIMRESULT_ITE',int2str(s),'_',char(vmType(i)),'_MEAN20_',int2str(brokerNum),'DEVICE_VM_LOAD.log');
                 readData = dlmread(filePathCreated,';',1,0);

                 results(i,:) = results(i,:) + transpose(readData(:,2));
             %end
         end
     end
     results = results/numOfSimulations;

    types = zeros(1,numberOfLines);
    for i=1:numberOfLines
        types(i)=i*numberOfLines;
    end
    
    hFig = figure;
    set(hFig, 'Position',getCloudSimConf(7));
    if(getCloudSimConf(20) == 1)
        plot(types, results(1,:),'-','MarkerFaceColor',getCloudSimConf(21),'color',getCloudSimConf(21),'LineWidth',1.5);
        hold on;
        plot(types, results(2,:),'-','MarkerFaceColor',getCloudSimConf(22),'color',getCloudSimConf(22),'LineWidth',1.5);
        %hold on;
        %plot(types, results(3,:),'-','MarkerFaceColor',getCloudSimConf(23),'color',getCloudSimConf(23),'LineWidth',1.5);
        
        set(gca,'color','none');
    else
        plot(types, results(1,:),'-','MarkerFaceColor','w','LineWidth',1.4);
        hold on;
        plot(types, results(2,:),'-','MarkerFaceColor','w','LineWidth',1.4);
        %hold on;
        %plot(types, results(3,:),'-','MarkerFaceColor','w','LineWidth',1.4);
        
        set(gcf, 'Position',getCloudSimConf(28));
    end
    lgnd = legend(getCloudSimConf(6),'Location','NorthEast');
    if(getCloudSimConf(20) == 1)
        set(lgnd,'color','none');
    end
    
    hold off;
    axis square
    xlabel('Time');
    %set(gca,'XTick', 1:1:numOfMobileDevices);
    ylabel(strcat('Number of Waiting Task on VM with "',int2str(brokerNum),'" Mobile Devices'));
    %set(gca,'YLim',[0 1]);
end