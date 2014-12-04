%--------------------------------------------------------------
%inputs
% argType: interger value which is equal to the followings
%           1=statistic folder
%           2=simulation time (in seconds)
%           3=Number of iterations
%           4=x tick interval for number of mobile devices
%           5=cloud vs cloudlet identifier used in file names
%           6=name of the lines on the figure
%           7=position of figure
%           8=server load log interval (in seconds)
%           9=Common text for s axis
%           10=min number of mobile device
%           11=step size of mobile device count
%           12=max number of mobile device
%           20=return if graph is plotted colerful
%           21=color of first line
%           22=color of second line
%           23=color of third line
%           24=color of fourth line
%
%description
% returns a value according to the given argumentssss
%--------------------------------------------------------------
function [ret_val] = getCloudSimConf(argType)
    if(argType == 1)
        ret_val = 'C:\Users\çagatay\Desktop\sim_results\';
    elseif(argType == 2)
        ret_val = 60 * 60 * 12; %simulation time (in seconds)
    elseif(argType == 3)
        ret_val = 5; %Number of iterations
    elseif(argType == 4)
        ret_val = 5; %x tick interval for number of mobile devices
    elseif(argType == 5)
        ret_val = {'CLOUD_WIFI_CLIENT','CLOUDLET_WIFI_CLIENT'};
    elseif(argType == 6)
        ret_val = {'Cloud','Cloudlet'};
    elseif(argType == 7)
        ret_val=[100 200 400 400]; %position of figure
    elseif(argType == 8)
        ret_val = 20; %server load log interval (in seconds)
    elseif(argType == 9)
        ret_val = 'Number of Mobile Devices'; %Common text for s axis
    elseif(argType == 10)
        ret_val = 1; %min number of mobile device
    elseif(argType == 11)
        ret_val = 1; %step size of mobile device count
    elseif(argType == 12)
        ret_val = 50; %max number of mobile device
    elseif(argType == 20)
        ret_val=1; %return if graph is plotted colerful
    elseif(argType == 21)
        ret_val=[0.55 0 0]; %color of first line
    elseif(argType == 22)
        ret_val=[0 0.15 0.6]; %color of second line
    elseif(argType == 23)
        ret_val=[0 0.23 0]; %color of third line
    elseif(argType == 24)
        ret_val=[0 0.75 0.3]; %color of fourth line
    end
end